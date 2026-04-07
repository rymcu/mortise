package com.rymcu.mortise.voice.infra.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rymcu.mortise.common.exception.ServiceException;
import com.rymcu.mortise.voice.kernel.config.VoiceProperties;
import com.rymcu.mortise.voice.kernel.model.AsrRequest;
import com.rymcu.mortise.voice.kernel.model.AsrResponse;
import com.rymcu.mortise.voice.kernel.model.TtsRequest;
import com.rymcu.mortise.voice.kernel.model.TtsResponse;
import com.rymcu.mortise.voice.kernel.model.VoiceRuntimeNodeStatus;
import com.rymcu.mortise.voice.kernel.spi.VoiceRuntimeClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于配置的运行时客户端实现。
 */
@Component
public class PropertiesBackedVoiceRuntimeClient implements VoiceRuntimeClient {

    private static final Pattern HEALTH_STATUS_PATTERN = Pattern.compile("\\\"status\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final List<String> DEFAULT_RECOGNIZE_PATHS = List.of(
            "/asr/recognize-once",
            "/recognize-once",
            "/api/v1/asr/recognize-once"
    );
        private static final List<String> DEFAULT_SYNTHESIZE_PATHS = List.of(
            "/tts/synthesize",
            "/synthesize",
            "/api/v1/tts/synthesize"
        );

    private final VoiceProperties voiceProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PropertiesBackedVoiceRuntimeClient(VoiceProperties voiceProperties) {
        this.voiceProperties = voiceProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(voiceProperties.runtime().connectTimeoutMillis()))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<VoiceRuntimeNodeStatus> listNodes() {
        return voiceProperties.runtime().nodes().stream()
                .map(this::probeNode)
                .toList();
    }

    @Override
    public boolean warmupModel(String runtimeName) {
        return voiceProperties.runtime().nodes().stream()
                .filter(VoiceProperties.RuntimeNode::enabled)
                .flatMap(node -> node.prewarmModels().stream())
                .anyMatch(runtimeName::equals);
    }

    @Override
    public AsrResponse recognizeOnce(AsrRequest request) {
        if (request == null || request.content() == null || request.content().length == 0) {
            throw new ServiceException("语音识别请求不能为空");
        }
        List<String> failures = new ArrayList<>();
        List<VoiceProperties.RuntimeNode> candidates = voiceProperties.runtime().nodes().stream()
                .filter(VoiceProperties.RuntimeNode::enabled)
                .filter(node -> StringUtils.hasText(node.baseUrl()))
                .toList();
        if (candidates.isEmpty()) {
            throw new ServiceException("未配置可用的语音运行时节点");
        }

        for (VoiceProperties.RuntimeNode node : candidates) {
            RecognizeAttempt attempt = recognizeOnce(node, request);
            if (attempt.response() != null) {
                return attempt.response();
            }
            failures.add(node.nodeId() + ": " + attempt.detail());
        }
        throw new ServiceException("短音频识别失败，所有节点均不可用: " + String.join("; ", failures));
    }

    @Override
    public TtsResponse synthesize(TtsRequest request) {
        if (request == null || !StringUtils.hasText(request.text())) {
            throw new ServiceException("语音合成请求不能为空");
        }
        List<String> failures = new ArrayList<>();
        List<VoiceProperties.RuntimeNode> candidates = voiceProperties.runtime().nodes().stream()
                .filter(VoiceProperties.RuntimeNode::enabled)
                .filter(node -> StringUtils.hasText(node.baseUrl()))
                .toList();
        if (candidates.isEmpty()) {
            throw new ServiceException("未配置可用的语音运行时节点");
        }

        for (VoiceProperties.RuntimeNode node : candidates) {
            SynthesizeAttempt attempt = synthesize(node, request);
            if (attempt.response() != null) {
                return attempt.response();
            }
            failures.add(node.nodeId() + ": " + attempt.detail());
        }
        throw new ServiceException("语音合成失败，所有节点均不可用: " + String.join("; ", failures));
    }

    private VoiceRuntimeNodeStatus probeNode(VoiceProperties.RuntimeNode node) {
        LocalDateTime checkedTime = LocalDateTime.now();
        if (!node.enabled()) {
            return new VoiceRuntimeNodeStatus(
                    node.nodeId(),
                    node.baseUrl(),
                    "disabled",
                    "skipped",
                    "节点已在静态配置中禁用，本次未执行健康检查",
                    null,
                    checkedTime,
                    node.prewarmModels()
            );
        }
        if (!StringUtils.hasText(node.baseUrl())) {
            return new VoiceRuntimeNodeStatus(
                    node.nodeId(),
                    node.baseUrl(),
                    "configured",
                    "invalid_config",
                    "节点已启用，但未配置 baseUrl，无法执行健康检查",
                    null,
                    checkedTime,
                    node.prewarmModels()
            );
        }

        ProbeOutcome lastOutcome = null;
        for (String healthUrl : resolveHealthUrls(node.baseUrl())) {
            ProbeOutcome outcome = probeHealthEndpoint(healthUrl);
            if (!outcome.retryNext()) {
                return toNodeStatus(node, checkedTime, outcome);
            }
            lastOutcome = outcome;
        }
        return toNodeStatus(
                node,
                checkedTime,
                lastOutcome != null
                        ? lastOutcome
                        : new ProbeOutcome("unreachable", "未找到可用的健康检查端点", null, false)
        );
    }

    private VoiceRuntimeNodeStatus toNodeStatus(
            VoiceProperties.RuntimeNode node,
            LocalDateTime checkedTime,
            ProbeOutcome outcome
    ) {
        return new VoiceRuntimeNodeStatus(
                node.nodeId(),
                node.baseUrl(),
                "configured",
                outcome.probeStatus(),
                outcome.detail(),
                outcome.latencyMillis(),
                checkedTime,
                node.prewarmModels()
        );
    }

    private ProbeOutcome probeHealthEndpoint(String healthUrl) {
        long startedAt = System.nanoTime();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(healthUrl))
                    .timeout(Duration.ofMillis(voiceProperties.runtime().readTimeoutMillis()))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latencyMillis = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            String runtimeStatus = extractHealthStatus(response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (!StringUtils.hasText(runtimeStatus) || "UP".equalsIgnoreCase(runtimeStatus)) {
                    return new ProbeOutcome(
                            "healthy",
                            "健康检查通过: " + healthUrl,
                            latencyMillis,
                            false
                    );
                }
                return new ProbeOutcome(
                        "unhealthy",
                        "健康检查返回状态 " + runtimeStatus + ": " + healthUrl,
                        latencyMillis,
                        false
                );
            }

            if (StringUtils.hasText(runtimeStatus)) {
                return new ProbeOutcome(
                        "unhealthy",
                        "健康检查返回 HTTP " + response.statusCode() + "，status=" + runtimeStatus + ": " + healthUrl,
                        latencyMillis,
                        false
                );
            }

            return new ProbeOutcome(
                    "unreachable",
                    "健康检查返回 HTTP " + response.statusCode() + ": " + healthUrl,
                    latencyMillis,
                    response.statusCode() == 404
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new ProbeOutcome(
                    "unreachable",
                    "健康检查被中断: " + exception.getMessage(),
                    null,
                    false
            );
        } catch (Exception exception) {
            return new ProbeOutcome(
                    "unreachable",
                    "健康检查失败: " + exception.getMessage(),
                    null,
                    false
            );
        }
    }

    private List<String> resolveHealthUrls(String baseUrl) {
        String normalized = normalizeBaseUrl(baseUrl);
        if (normalized.endsWith("/actuator/health") || normalized.endsWith("/health")) {
            return List.of(normalized);
        }
        return List.of(normalized + "/actuator/health", normalized + "/health");
    }

    private RecognizeAttempt recognizeOnce(VoiceProperties.RuntimeNode node, AsrRequest request) {
        RecognizeAttempt lastAttempt = null;
        for (String recognizeUrl : resolveRecognizeUrls(node.baseUrl())) {
            RecognizeAttempt attempt = recognizeEndpoint(recognizeUrl, request);
            if (attempt.response() != null || !attempt.retryNext()) {
                return attempt;
            }
            lastAttempt = attempt;
        }
        return lastAttempt != null
                ? lastAttempt
                : new RecognizeAttempt(null, "未找到可用的识别端点", false);
    }

    private SynthesizeAttempt synthesize(VoiceProperties.RuntimeNode node, TtsRequest request) {
        SynthesizeAttempt lastAttempt = null;
        for (String synthesizeUrl : resolveSynthesizeUrls(node.baseUrl())) {
            SynthesizeAttempt attempt = synthesizeEndpoint(synthesizeUrl, request);
            if (attempt.response() != null || !attempt.retryNext()) {
                return attempt;
            }
            lastAttempt = attempt;
        }
        return lastAttempt != null
                ? lastAttempt
                : new SynthesizeAttempt(null, "未找到可用的语音合成端点", false);
    }

    private RecognizeAttempt recognizeEndpoint(String recognizeUrl, AsrRequest request) {
        try {
            String boundary = "----MortiseVoiceBoundary" + UUID.randomUUID().toString().replace("-", "");
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(recognizeUrl))
                    .timeout(Duration.ofMillis(voiceProperties.runtime().readTimeoutMillis()))
                    .header("Accept", "application/json, text/plain;q=0.9")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(buildMultipartBody(boundary, request)))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new RecognizeAttempt(parseAsrResponse(response.body()), "OK", false);
            }
            if (response.statusCode() == 404) {
                return new RecognizeAttempt(null, "识别端点不存在: " + recognizeUrl, true);
            }
            return new RecognizeAttempt(null, "识别端点返回 HTTP " + response.statusCode() + ": " + extractErrorMessage(response.body()), false);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new RecognizeAttempt(null, "识别请求被中断: " + exception.getMessage(), false);
        } catch (Exception exception) {
            return new RecognizeAttempt(null, "识别请求失败: " + exception.getMessage(), false);
        }
    }

    private SynthesizeAttempt synthesizeEndpoint(String synthesizeUrl, TtsRequest request) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(synthesizeUrl))
                    .timeout(Duration.ofMillis(voiceProperties.runtime().readTimeoutMillis()))
                    .header("Accept", "application/json, audio/*;q=0.9, text/plain;q=0.8")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(buildSynthesizeRequestBody(request), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<byte[]> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new SynthesizeAttempt(parseTtsResponse(response), "OK", false);
            }
            if (response.statusCode() == 404) {
                return new SynthesizeAttempt(null, "语音合成端点不存在: " + synthesizeUrl, true);
            }
            return new SynthesizeAttempt(
                    null,
                    "语音合成端点返回 HTTP " + response.statusCode() + ": " + extractErrorMessage(asUtf8(response.body())),
                    false
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new SynthesizeAttempt(null, "语音合成请求被中断: " + exception.getMessage(), false);
        } catch (Exception exception) {
            return new SynthesizeAttempt(null, "语音合成请求失败: " + exception.getMessage(), false);
        }
    }

    private byte[] buildMultipartBody(String boundary, AsrRequest request) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeTextPart(outputStream, boundary, "profileCode", request.profileCode());
        writeTextPart(outputStream, boundary, "fileName", request.fileName());
        writeTextPart(outputStream, boundary, "contentType", request.contentType());
        writeFilePart(outputStream, boundary, "file", request.fileName(), request.contentType(), request.content());
        outputStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return outputStream.toByteArray();
    }

    private void writeTextPart(ByteArrayOutputStream outputStream, String boundary, String name, String value) throws IOException {
        if (!StringUtils.hasText(value)) {
            return;
        }
        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write(value.getBytes(StandardCharsets.UTF_8));
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void writeFilePart(
            ByteArrayOutputStream outputStream,
            String boundary,
            String name,
            String fileName,
            String contentType,
            byte[] content
    ) throws IOException {
        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write((
                "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"\r\n"
                        + "Content-Type: " + contentType + "\r\n\r\n"
        ).getBytes(StandardCharsets.UTF_8));
        outputStream.write(content);
        outputStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private List<String> resolveRecognizeUrls(String baseUrl) {
        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
        Set<String> urls = new LinkedHashSet<>();
        urls.add(joinUrl(normalizedBaseUrl, voiceProperties.asr().recognizePath()));
        for (String path : DEFAULT_RECOGNIZE_PATHS) {
            urls.add(joinUrl(normalizedBaseUrl, path));
        }
        return List.copyOf(urls);
    }

    private List<String> resolveSynthesizeUrls(String baseUrl) {
        String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
        Set<String> urls = new LinkedHashSet<>();
        urls.add(joinUrl(normalizedBaseUrl, voiceProperties.tts().synthesizePath()));
        for (String path : DEFAULT_SYNTHESIZE_PATHS) {
            urls.add(joinUrl(normalizedBaseUrl, path));
        }
        return List.copyOf(urls);
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl.strip();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String joinUrl(String baseUrl, String path) {
        String normalizedPath = StringUtils.hasText(path) ? path.strip() : "/";
        if (normalizedPath.startsWith("http://") || normalizedPath.startsWith("https://")) {
            return normalizedPath;
        }
        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }
        return baseUrl + normalizedPath;
    }

    private AsrResponse parseAsrResponse(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            throw new ServiceException("语音运行时返回空响应");
        }
        String trimmed = responseBody.strip();
        try {
            JsonNode root = objectMapper.readTree(trimmed);
            if (root.has("success") && !root.path("success").asBoolean(true)) {
                throw new ServiceException(extractErrorMessage(trimmed));
            }
            JsonNode payload = unwrapPayload(root);
            String text = firstText(payload, "text", "result", "transcript");
            if (!StringUtils.hasText(text) && payload != root) {
                text = firstText(root, "text", "result", "transcript");
            }
            if (!StringUtils.hasText(text)) {
                throw new ServiceException("语音运行时响应中缺少识别文本");
            }
            return new AsrResponse(
                    firstLong(payload, "jobId", "job_id"),
                    firstLong(payload, "artifactId", "artifact_id"),
                    text,
                    firstText(payload, "language"),
                    firstDouble(payload, "durationSeconds", "duration_seconds", "duration"),
                    firstStringList(payload, "tokens"),
                    firstDoubleList(payload, "timestamps")
            );
        } catch (JsonProcessingException exception) {
            return new AsrResponse(null, null, trimmed, null, null, List.of(), List.of());
        }
    }

    private String buildSynthesizeRequestBody(TtsRequest request) throws JsonProcessingException {
        com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();
        root.put("profileCode", request.profileCode());
        root.put("text", request.text());
        if (StringUtils.hasText(request.voiceName())) {
            root.put("voiceName", request.voiceName());
        }
        return objectMapper.writeValueAsString(root);
    }

    private TtsResponse parseTtsResponse(HttpResponse<byte[]> response) {
        byte[] responseBody = response.body();
        if (responseBody == null || responseBody.length == 0) {
            throw new ServiceException("语音运行时返回空响应");
        }
        String contentType = response.headers().firstValue("Content-Type").orElse(null);
        if (isAudioContentType(contentType)) {
            return new TtsResponse(
                    null,
                    null,
                    inferFormat(null, contentType),
                    null,
                    normalizeMimeType(contentType),
                    responseBody
            );
        }

        String textBody = asUtf8(responseBody).strip();
        if (!StringUtils.hasText(textBody)) {
            throw new ServiceException("语音运行时返回空响应");
        }
        try {
            JsonNode root = objectMapper.readTree(textBody);
            if (root.has("success") && !root.path("success").asBoolean(true)) {
                throw new ServiceException(extractErrorMessage(textBody));
            }
            JsonNode payload = unwrapPayload(root);
            String downloadUrl = firstText(payload, "downloadUrl", "download_url", "url", "fileUrl", "file_url");
            if (!StringUtils.hasText(downloadUrl) && payload != root) {
                downloadUrl = firstText(root, "downloadUrl", "download_url", "url", "fileUrl", "file_url");
            }
            String format = firstText(payload, "format", "audioFormat", "audio_format", "ext", "extension");
            if (!StringUtils.hasText(format) && payload != root) {
                format = firstText(root, "format", "audioFormat", "audio_format", "ext", "extension");
            }
            String mimeType = firstText(payload, "contentType", "content_type", "mimeType", "mime_type");
            if (!StringUtils.hasText(mimeType) && payload != root) {
                mimeType = firstText(root, "contentType", "content_type", "mimeType", "mime_type");
            }
            byte[] content = firstDecodedBytes(payload, "audioBase64", "audio_base64", "base64", "contentBase64", "content_base64");
            if ((content == null || content.length == 0) && payload != root) {
                content = firstDecodedBytes(root, "audioBase64", "audio_base64", "base64", "contentBase64", "content_base64");
            }
            if (!StringUtils.hasText(downloadUrl) && (content == null || content.length == 0)) {
                throw new ServiceException("语音运行时响应中缺少音频内容或下载地址");
            }
            return new TtsResponse(
                    firstLong(payload, "jobId", "job_id"),
                    firstLong(payload, "artifactId", "artifact_id"),
                    inferFormat(format, mimeType),
                    downloadUrl,
                    normalizeMimeType(mimeType),
                    content
            );
        } catch (JsonProcessingException exception) {
            if (textBody.startsWith("http://") || textBody.startsWith("https://")) {
                return new TtsResponse(null, null, null, textBody, null, null);
            }
            throw new ServiceException("语音运行时返回无法解析的 TTS 响应");
        }
    }

    private JsonNode unwrapPayload(JsonNode root) {
        if (root.has("data") && root.get("data").isObject()) {
            return root.get("data");
        }
        if (root.has("result") && root.get("result").isObject()) {
            return root.get("result");
        }
        return root;
    }

    private String extractErrorMessage(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "语音运行时返回空错误信息";
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String message = firstText(root, "message", "msg", "error");
            return StringUtils.hasText(message) ? message : responseBody.strip();
        } catch (Exception exception) {
            return responseBody.strip();
        }
    }

    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                String value = node.get(fieldName).asText();
                if (StringUtils.hasText(value)) {
                    return value;
                }
            }
        }
        return null;
    }

    private byte[] firstDecodedBytes(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (!node.has(fieldName) || node.get(fieldName).isNull()) {
                continue;
            }
            String value = node.get(fieldName).asText();
            if (!StringUtils.hasText(value)) {
                continue;
            }
            try {
                return Base64.getDecoder().decode(value.strip());
            } catch (IllegalArgumentException ignored) {
                // ignore invalid base64 and continue
            }
        }
        return null;
    }

    private Long firstLong(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && node.get(fieldName).canConvertToLong()) {
                return node.get(fieldName).asLong();
            }
        }
        return null;
    }

    private Double firstDouble(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && node.get(fieldName).isNumber()) {
                return node.get(fieldName).asDouble();
            }
            if (node.has(fieldName) && node.get(fieldName).isTextual()) {
                try {
                    return Double.parseDouble(node.get(fieldName).asText());
                } catch (NumberFormatException ignored) {
                    // ignore invalid numeric text and continue
                }
            }
        }
        return null;
    }

    private List<String> firstStringList(JsonNode node, String fieldName) {
        if (!node.has(fieldName) || !node.get(fieldName).isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        node.get(fieldName).forEach(item -> result.add(item.asText()));
        return result;
    }

    private List<Double> firstDoubleList(JsonNode node, String fieldName) {
        if (!node.has(fieldName) || !node.get(fieldName).isArray()) {
            return List.of();
        }
        List<Double> result = new ArrayList<>();
        node.get(fieldName).forEach(item -> result.add(item.asDouble()));
        return result;
    }

    private String extractHealthStatus(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }
        Matcher matcher = HEALTH_STATUS_PATTERN.matcher(responseBody);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    private boolean isAudioContentType(String contentType) {
        return StringUtils.hasText(contentType) && normalizeMimeType(contentType).startsWith("audio/");
    }

    private String inferFormat(String format, String contentType) {
        if (StringUtils.hasText(format)) {
            return format.strip().toLowerCase(Locale.ROOT);
        }
        String mimeType = normalizeMimeType(contentType);
        if (StringUtils.hasText(mimeType) && mimeType.contains("/")) {
            return mimeType.substring(mimeType.indexOf('/') + 1).toLowerCase(Locale.ROOT);
        }
        return null;
    }

    private String normalizeMimeType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return null;
        }
        String normalized = contentType.strip();
        int separator = normalized.indexOf(';');
        if (separator >= 0) {
            normalized = normalized.substring(0, separator);
        }
        return normalized;
    }

    private String asUtf8(byte[] content) {
        return content == null ? "" : new String(content, StandardCharsets.UTF_8);
    }

    private record ProbeOutcome(
            String probeStatus,
            String detail,
            Long latencyMillis,
            boolean retryNext
    ) {
    }

    private record RecognizeAttempt(
            AsrResponse response,
            String detail,
            boolean retryNext
    ) {
    }

    private record SynthesizeAttempt(
            TtsResponse response,
            String detail,
            boolean retryNext
    ) {
    }
}
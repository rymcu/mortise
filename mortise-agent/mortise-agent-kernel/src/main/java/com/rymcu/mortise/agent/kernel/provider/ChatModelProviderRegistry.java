package com.rymcu.mortise.agent.kernel.provider;

import com.rymcu.mortise.agent.kernel.model.ModelType;
import com.rymcu.mortise.agent.kernel.spi.ChatModelProvider;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型提供者注册表。
 */
public class ChatModelProviderRegistry {

    private final Map<ModelType, ChatModelProvider> providers = new ConcurrentHashMap<>();
    private ModelType defaultProvider;

    public void register(ChatModelProvider provider) {
        Objects.requireNonNull(provider, "Provider cannot be null");
        providers.put(provider.getModelType(), provider);
    }

    public void unregister(ModelType modelType) {
        providers.remove(modelType);
    }

    public Optional<ChatModelProvider> getProvider(ModelType modelType) {
        return Optional.ofNullable(providers.get(modelType));
    }

    public ChatModelProvider getRequiredProvider(ModelType modelType) {
        return getProvider(modelType)
                .orElseThrow(() -> new IllegalArgumentException("No provider available for: " + modelType));
    }

    public ChatModelProvider getDefaultProvider() {
        if (defaultProvider != null && providers.containsKey(defaultProvider)) {
            return providers.get(defaultProvider);
        }
        return providers.values().stream()
                .filter(ChatModelProvider::isAvailable)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No available chat model provider"));
    }

    public void setDefaultProvider(ModelType modelType) {
        this.defaultProvider = modelType;
    }

    public List<ChatModelProvider> getAvailableProviders() {
        return providers.values().stream()
                .filter(ChatModelProvider::isAvailable)
                .toList();
    }

    public Set<ModelType> getRegisteredTypes() {
        return Set.copyOf(providers.keySet());
    }
}

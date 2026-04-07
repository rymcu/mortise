import io
import logging
import os
import threading

import sherpa_onnx
import soundfile as sf
from fastapi import FastAPI, File, Form, HTTPException, UploadFile


def parse_bool(value: str | None, default: bool = True) -> bool:
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


logging.basicConfig(
    level=os.getenv("LOG_LEVEL", "INFO").upper(),
    format="%(asctime)s %(levelname)s [voice-runtime] %(message)s",
)
logger = logging.getLogger("voice-runtime")

model_alias = os.getenv("SENSEVOICE_MODEL_ALIAS", "sense-voice")
model_path = os.environ["SENSEVOICE_MODEL"]
tokens_path = os.environ["SENSEVOICE_TOKENS"]
language = os.getenv("SENSEVOICE_LANGUAGE", "auto")
use_itn = parse_bool(os.getenv("SENSEVOICE_USE_ITN"), True)

logger.info("loading recognizer model_alias=%s model=%s", model_alias, model_path)
recognizer = sherpa_onnx.OfflineRecognizer.from_sense_voice(
    model=model_path,
    tokens=tokens_path,
    language=language,
    use_itn=use_itn,
    debug=False,
)
recognizer_lock = threading.Lock()

app = FastAPI(title="Mortise Voice Runtime", version="1.0.0")


@app.get("/health")
def health() -> dict:
    return {
        "status": "UP",
        "engine": "sherpa-onnx",
        "modelAlias": model_alias,
    }


@app.post("/asr/recognize-once")
async def recognize_once(
    profileCode: str = Form(...),
    file: UploadFile = File(...),
    fileName: str | None = Form(None),
    contentType: str | None = Form(None),
) -> dict:
    payload = await file.read()
    if not payload:
        raise HTTPException(status_code=400, detail="音频文件不能为空")

    try:
        waveform, sample_rate = sf.read(io.BytesIO(payload), dtype="float32", always_2d=True)
    except Exception as exception:
        raise HTTPException(status_code=400, detail=f"无法解析音频文件: {exception}") from exception

    samples = waveform[:, 0]

    try:
        with recognizer_lock:
            stream = recognizer.create_stream()
            stream.accept_waveform(sample_rate, samples)
            recognizer.decode_stream(stream)
            result = stream.result
    except Exception as exception:
        logger.exception("recognize failed profileCode=%s fileName=%s contentType=%s", profileCode, fileName, contentType)
        raise HTTPException(status_code=500, detail=f"识别失败: {exception}") from exception

    text = result.text.strip()
    logger.info(
        "recognized profileCode=%s fileName=%s sampleRate=%s textLength=%s",
        profileCode,
        fileName or file.filename,
        sample_rate,
        len(text),
    )
    return {
        "text": text,
        "language": getattr(result, "lang", None) or None,
    }

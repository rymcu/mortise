FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1 \
    PIP_INDEX_URL=https://pypi.org/simple \
    PIP_DEFAULT_TIMEOUT=120

RUN apt-get update \
    && apt-get install -y --no-install-recommends libsndfile1 \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --create-home --uid 10001 appuser

WORKDIR /app

COPY requirements.txt ./
RUN pip install -r requirements.txt

COPY app.py ./

RUN mkdir -p /logs \
    && chown -R appuser:appuser /app /logs

USER appuser

EXPOSE 7778

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "7778", "--workers", "1", "--proxy-headers", "--forwarded-allow-ips", "127.0.0.1"]

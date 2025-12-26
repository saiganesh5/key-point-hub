import requests

url = "http://127.0.0.1:8000/extract-pose"
with open("IMG_20230121_122004.jpg", "rb") as f:
    files = {"file": ("IMG_20230121_122004.jpg", f, "image/jpeg")}
    r = requests.post(url, files=files, timeout=30)
print("status:", r.status_code)
print("content-type:", r.headers.get("content-type"))
print("text (first 2000 chars):")
print(r.text[:2000])


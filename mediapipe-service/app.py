# app.py
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
import uvicorn
import numpy as np
from PIL import Image
import io
import mediapipe as mp
import traceback
import logging

logger = logging.getLogger("mediapipe_debug")
logging.basicConfig(level=logging.INFO)

app = FastAPI(title="MediaPipe Pose Service")

mp_pose = mp.solutions.pose
mp_drawing = mp.solutions.drawing_utils

def read_imagefile(file_bytes) -> np.ndarray:
    # read bytes -> PIL -> RGB numpy array
    image = Image.open(io.BytesIO(file_bytes)).convert("RGB")
    arr = np.array(image)
    return arr

def normalize_image(image_np: np.ndarray, max_side=1024) -> np.ndarray:
    # ensure shape, dtype and optionally resize large images to avoid memory/timeouts
    if image_np is None:
        raise ValueError("image is None")
    if image_np.ndim != 3 or image_np.shape[2] != 3:
        raise ValueError(f"Expected HxWx3 image, got shape {getattr(image_np, 'shape', None)}")
    # convert dtype
    if image_np.dtype != np.uint8:
        # assume float in 0-1 or other and convert
        image_np = (image_np * 255).astype(np.uint8) if image_np.max() <= 1.0 else image_np.astype(np.uint8)
    # resize if too large (preserve an aspect ratio)
    h, w = image_np.shape[:2]
    side = max(h, w)
    if side > max_side:
        scale = max_side / side
        new_w = int(w * scale)
        new_h = int(h * scale)
        image_pil = Image.fromarray(image_np)
        image_pil = image_pil.resize((new_w, new_h), Image.LANCZOS)
        image_np = np.array(image_pil)
    # Make contiguous and writable flag ok for mediapipe
    image_np = np.ascontiguousarray(image_np)
    return image_np

@app.post("/extract-pose")
async def extract_pose(file: UploadFile = File(...)):
    try:
        if not file.content_type.startswith("image/"):
            raise HTTPException(status_code=400, detail="File must be an image")

        contents = await file.read()
        image_np = read_imagefile(contents)
        image_np = normalize_image(image_np, max_side=1024)

        # debug logs (will show in the server console)
        logger.info("Received image shape=%s dtype=%s", image_np.shape, image_np.dtype)

        # Use static_image_mode=True for single images
        with mp_pose.Pose(static_image_mode=True, model_complexity=2) as pose:
            # MediaPipe expects RGB uint8 numpy array
            results = pose.process(image_np)

            if not results or not results.pose_landmarks:
                return JSONResponse({"keypoints": [], "num_keypoints": 0})

            keypoints = []
            for idx, lm in enumerate(results.pose_landmarks.landmark):
                keypoints.append({
                    "index": idx,
                    "x": float(lm.x),
                    "y": float(lm.y),
                    "z": float(lm.z),
                    "visibility": float(lm.visibility)
                })

            return JSONResponse({"keypoints": keypoints, "num_keypoints": len(keypoints)})

    except Exception as exc:
        tb = traceback.format_exc()
        logger.error("Error in /extract-pose:\n%s", tb)
        # return traceback to a client for debugging (remove or limit in production)
        return JSONResponse({"error": str(exc), "traceback": tb}, status_code=500)


if __name__ == "__main__":
    # For local dev, prefer using the CLI:
    # python -m uvicorn app:app --reload
    uvicorn.run(app, host="0.0.0.0", port=8000)






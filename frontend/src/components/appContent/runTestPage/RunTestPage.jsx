import { useEffect, useRef } from "react";
import EyeGestures from "eyegestures";

export default function RunTestPage() {
  const videoRef = useRef(null);

  const startCamera = async () => {
    try {
      if (videoRef.current && videoRef.current.srcObject) {
        videoRef.current.srcObject.getTracks().forEach((track) => track.stop());
        videoRef.current.srcObject = null;
      }

      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      if (videoRef.current) videoRef.current.srcObject = stream;
    } catch (err) {
      console.error("Brak dostępu do kamery", err);
    }
  };

  // Closing a camera takes a few seconds!
  const stopCamera = () => {
    if (videoRef.current && videoRef.current.srcObject) {
      videoRef.current.srcObject.getTracks().forEach((track) => track.stop());
      videoRef.current.srcObject = null;
    }
  };

  useEffect(() => {
    startCamera();
    return () => stopCamera();
  }, []);

  return (
    <div style={{ padding: "2rem" }}>
      <h1>Run Test Page</h1>
      <p>Tutaj możesz uruchomić swój algorytm EyeGestures lub inne testy.</p>
      <video
        ref={videoRef}
        id="video_element_id"
        width="640"
        height="480"
        autoPlay
        muted
        style={{ display: "block" }}
      ></video>
      <div id="status" style={{ display: "none" }}>
        Initializing...
      </div>
      <div id="error" style={{ display: "none" }}></div>
      <button onClick={stopCamera}>Zamknij kamerę</button>
    </div>
  );
}

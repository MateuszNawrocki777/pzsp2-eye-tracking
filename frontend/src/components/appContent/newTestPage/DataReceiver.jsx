import { useEffect, useState } from "react";

export default function DataReceiver() {
  const [gazePoints, setGazePoints] = useState([]);

  useEffect(() => {
    const handleMessage = (event) => {
      if (event.origin !== window.location.origin) return;
      const { point, calibration } = event.data;
      setGazePoints((prev) => [...prev, { point, calibration }]);
    };

    window.addEventListener("message", handleMessage);
    return () => window.removeEventListener("message", handleMessage);
  }, []);

  return (
    <div>
      <h2>Otrzymane punkty:</h2>
      <ul>
        {gazePoints.map((g, index) => (
          <li key={index}>
            {/* x: {g.point[0]}, y: {g.point[1]}, calibrated:{" "}
            {g.calibration ? "yes" : "no"} */}
            x: {g.point[0]}, y: {g.point[1]}
          </li>
        ))}
      </ul>
    </div>
  );
}

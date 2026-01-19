import { useEffect, useRef, useState } from "react";
import simpleheat from "simpleheat";

import "./Heatmap.css";


const heatmapRadiusMultiplier = 50 / 1800;
const heatmapValueMultiplier = 0.025;


export default function Heatmap({ image, points }) {
    const canvasRef = useRef(null);
    const imgRef = useRef(null);
    const [ready, setReady] = useState(false);

    useEffect(() => {
        if (!ready) return;

        const canvas = canvasRef.current;
        if (!canvas) return;

        const rect = canvas.getBoundingClientRect();

        if (rect.width === 0 || rect.height === 0) return;

        canvas.width = rect.width;
        canvas.height = rect.height;

        const heat = simpleheat(canvas);
        const radius = rect.width * heatmapRadiusMultiplier;

        heat.radius(radius, radius / 2);

        const pointsValuesSum = points.reduce((sum, p) => sum + p[2], 0);
        heat.max(pointsValuesSum * heatmapValueMultiplier);

        const data = points.map(([x, y, val]) => [
            x * rect.width,
            y * rect.height,
            val
        ]);

        heat.data(data);
        heat.draw();
    }, [points, ready]);

    return (
        <div className="heatmap-container">
            <img
                ref={imgRef}
                src={image}
                alt="Heatmap"
                className="heatmap-image"
                onLoad={() => setReady(true)}
            />
            <canvas ref={canvasRef} className="heatmap-canvas" />
        </div>
    );
}

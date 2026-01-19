import { useEffect, useRef } from "react";
import simpleheat from "simpleheat";

import "./Heatmap.css";


const heatmapRadiusMultiplier = 50 / 1800;
const heatmapValueMultiplier = 0.025;


export default function Heatmap({ image, points }) {
    const canvasRef = useRef(null);

    useEffect(() => {
        const canvas = canvasRef.current;
        if (!canvas) return;

        const rect = canvas.getBoundingClientRect();
        canvas.width = rect.width;
        canvas.height = rect.height;

        const radius = rect.width * heatmapRadiusMultiplier;

        const heat = simpleheat(canvas);

        heat.radius(radius, radius / 2);
        const pointsValuesSum = points.reduce((sum, p) => sum + p[2], 0);
        heat.max(pointsValuesSum * heatmapValueMultiplier);

        const data = points.map(([x, y, val]) => [x * rect.width, y * rect.height, val]);

        heat.data(data);
        heat.draw();
    }, [points]);

    return (
        <div className="heatmap-container">
            <img src={image} alt="Heatmap" className="heatmap-image" />
            <canvas ref={canvasRef} className="heatmap-canvas" />
        </div>
    );
}

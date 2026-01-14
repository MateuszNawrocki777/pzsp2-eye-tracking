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
        heat.max(points.length * heatmapValueMultiplier);

        const data = points.map(([x, y]) => [x * rect.width, y * rect.height, 1]);

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

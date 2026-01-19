import PopupMessage from "../popupMessage/PopupMessage";
import Heatmap from "./Heatmap";

import "./HeatmapPopup.css";


export default function HeatmapPopup({ image, points, onClose }) {
    return (
        <PopupMessage onClickOutside={onClose}>
            <div className="heatmap-popup-container">
                <div className="heatmap-popup-image-container">
                    <Heatmap image={image} points={points} />
                </div>
                <button
                    className="heatmap-popup-close-button"
                    onClick={() => onClose()}
                    >
                    <span className="material-symbols-outlined">close</span>
                </button>
            </div>
        </PopupMessage>
    );
}
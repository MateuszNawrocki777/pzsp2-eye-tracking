import "./ImageThumbnailWithButtons.css";
import "./ImageThumbnail.css";


export default function ImageThumbnailWithButtons({ image, buttons }) {
    return (
        <div className="image-thumbnail-with-buttons-container">
            <img src={image} alt="Thumbnail" className="image-thumbnail" />
            <div className="image-thumbnail-buttons-container">
                {buttons}
            </div>
        </div>
    );
}
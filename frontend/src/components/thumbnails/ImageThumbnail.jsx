import "./ImageThumbnail.css";


export default function ImageThumbnail({ image }) {
    return (
        <img src={image} alt="Thumbnail" className="image-thumbnail" />
    );
}
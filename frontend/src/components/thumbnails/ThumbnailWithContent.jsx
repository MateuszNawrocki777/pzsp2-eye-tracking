import "./ThumbnailWithContent.css";


export default function ThumbnailWithContent({ image, children, ...props }) {
    return (
        <div className="thumbnail-with-content-container" {...props}>
            <div className="thumbnail-with-content-image">{image}</div>
            {children && <div className="thumbnail-with-content-content">{children}</div>}
        </div>
    );
}
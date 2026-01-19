import ImageThumbnail from "../thumbnails/ImageThumbnail";
import ThumbnailWithContent from "../thumbnails/ThumbnailWithContent";

import "./TestResultImages.css";


export default function TestResultImages({ images, setIndex }) {
    return (
      <div className="test-result-images-container">
        {images.map((image, index) => (
          <ImageCard key={index} image={image} idx={index} setIndex={setIndex} />
        ))}
      </div>
    );
}


function ImageCard({ image, idx, setIndex }) {
    return (
      <ThumbnailWithContent
        image={<ImageThumbnail image={image} buttons={null} />}
        className="result-images-thumbnail-with-content-container"
        onClick={() => setIndex(idx)}
      />
    );
}
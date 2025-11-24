import ThumbnailWithContent from "../../thumbnails/ThumbnailWithContent";
import CreateThumbnail from "../../thumbnails/CreateThumbnail";

import "./NewTestPage.css";


export default function NewTestPage() {
    return (
        <div className="new-test-container">
            <div className="new-test-content-container">
                <h1>New Test</h1>
                <div className="new-test-images-container">
                    <ThumbnailWithContent image={<div className="new-test-add-image-thumbnail"><CreateThumbnail /></div>}>
                        Add Image
                        <button>Add</button>
                    </ThumbnailWithContent>
                </div>
            </div>
        </div>
    );
}
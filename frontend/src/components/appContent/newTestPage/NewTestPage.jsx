import { useState, useRef } from "react";

import ThumbnailWithContent from "../../thumbnails/ThumbnailWithContent";
import CreateThumbnail from "../../thumbnails/CreateThumbnail";

import "./NewTestPage.css";


export default function NewTestPage() {
    const [images, setImages] = useState([]); {/* TODO: Lift this state up into a context, so the progress is saved, when exiting this tab */}
    const fileInputRef = useRef(null);

    function handleAddImageClick() {
        fileInputRef.current.click();
    }

    function handleFileInputChange(event) {
        const files = Array.from(event.target.files);
        const newImages = files.map((file) => URL.createObjectURL(file));
        setImages((prevImages) => [...prevImages, ...newImages]);
        event.target.value = null; 
    }

    return (
        <div className="new-test-container">
            <div className="new-test-content-container">
                <h1>New Test</h1>
                <div className="new-test-images-container">
                    <AddImageCard />
                </div>
            </div>
            <input
                type="file"
                accept=".png,.jpg,.jpeg,.pdf"
                multiple
                ref={fileInputRef}
                onChange={handleFileInputChange}
                style={{ display: "none" }}
            />
        </div>
    );

    function AddImageThumbnail() {
        return (
            <div className="new-test-add-image-thumbnail" onClick={handleAddImageClick}>
                <CreateThumbnail />
            </div>
        );
    }

    function AddImageCard() {
        return (
            <ThumbnailWithContent image={<AddImageThumbnail />}>
                <button onClick={handleAddImageClick}>Add image</button>
                <button className="new-test-add-image-text">.png .jpg .pdf</button>
            </ThumbnailWithContent>
        );
    }
}
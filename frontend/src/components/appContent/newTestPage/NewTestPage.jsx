import { useState, useRef } from "react";

import ThumbnailWithContent from "../../thumbnails/ThumbnailWithContent";
import CreateThumbnail from "../../thumbnails/CreateThumbnail";
import ImageThumbnailWithButtons from "../../thumbnails/ImageThumbnailWithButtons";

import "./NewTestPage.css";


export default function NewTestPage() {
    const [images, setImages] = useState([]); {/* TODO: Lift this state up into a context, so the progress is saved, when exiting this tab */}
    
    const fileInputRef = useRef(null);
    const gazeTrackingInputRef = useRef(null);
    const displayTimeInputRef = useRef(null);
    const timeInputRef = useRef(null);
    const randomizeImagesInputRef = useRef(null);

    function handleAddImageClick() {
        fileInputRef.current.click();
    }

    function handleFileInputChange(event) {
        const files = Array.from(event.target.files);
        const newImages = files.map((file) => URL.createObjectURL(file));
        setImages((prevImages) => [...prevImages, ...newImages]);
        event.target.value = null; 
    }

    function handleRemoveImage(image) {
        setImages((prevImages) => prevImages.filter((img) => img !== image));
    }

    return (
        <div className="new-test-container">
            <div className="new-test-content-container">
                <div>
                    <h1>New Test</h1>
                    <ImageCards />
                </div>
                <div className="new-test-control-container">
                    <Settings />
                    <div className="new-test-control-panel">
                        <h2>Finish Test</h2>
                    </div>
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
            <ThumbnailWithContent image={<AddImageThumbnail />} />
        );
    }

    function ImageCard({ image }) {
        const [hovered, setHovered] = useState(false);

        const buttons = hovered && (
            <>
                {/* <button className="new-test-preview-image-button">
                    <span className="material-symbols-outlined">
                    visibility
                    </span>
                </button> TODO: Make this button functional */}
                <button className="new-test-remove-image-button"
                    onClick={() => handleRemoveImage(image)}>
                    <span className="material-symbols-outlined">
                    close
                    </span>
                </button>
            </>
        );

        return (
            <ThumbnailWithContent 
                image={<ImageThumbnailWithButtons image={image} buttons={buttons}/>}
                onMouseEnter={() => setHovered(true)}
                onMouseLeave={() => setHovered(false)} />
        );
    }

    function ImageCards() {
        return (
            <div className="new-test-images-container">
                <AddImageCard />
                {images.map((image, index) => (
                    <ImageCard key={index} image={image} />
                ))}
            </div>
        );
    }

    function Settings() {
        return (
            <div className="new-test-control-panel">
                <h2>Settings</h2>
                <div className="new-test-control-checkbox">
                    <input 
                        type="checkbox" 
                        id="displayGaze" 
                        name="displayGaze"
                        ref={gazeTrackingInputRef} />
                    <label htmlFor="displayGaze"> Display Gaze Tracking </label>
                </div>
                <div className="new-test-control-checkbox">
                    <input 
                        type="checkbox" 
                        id="displayTimeLeft" 
                        name="displayTimeLeft"
                        ref={displayTimeInputRef} />
                    <label htmlFor="displayTimeLeft"> Display Time Left </label>
                </div>
                <div className="new-test-control-checkbox">
                    <input 
                        className="new-test-time-input"
                        type="number" 
                        id="imageTime" 
                        name="imageTime"
                        ref={timeInputRef} />
                    <label htmlFor="imageTime"> seconds per image </label>
                </div>
                <div className="new-test-control-checkbox">
                    <input 
                        type="checkbox" 
                        id="randomizeImages" 
                        name="randomizeImages"
                        ref={randomizeImagesInputRef} />
                    <label htmlFor="randomizeImages"> Randomize Image Order </label>
                </div>
            </div>
        );
    }
}
import { useState, useRef } from "react";

import { useNewTest } from "../../../hooks/newTestContext";

import ThumbnailWithContent from "../../thumbnails/ThumbnailWithContent";
import CreateThumbnail from "../../thumbnails/CreateThumbnail";
import ImageThumbnailWithButtons from "../../thumbnails/ImageThumbnailWithButtons";
import PopupMessage from "../../popupMessage/PopupMessage";

import "./NewTestPage.css";


export default function NewTestPage() {
    const { images, setImages } = useNewTest();

    const [showResetPopup, setShowResetPopup] = useState(false);
    
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
                <button className="new-test-reset-button" onClick={() => setShowResetPopup(true)}>
                    Reset
                </button>
            </div>
            <input
                type="file"
                accept=".png,.jpg,.jpeg,.pdf"
                multiple
                ref={fileInputRef}
                onChange={handleFileInputChange}
                style={{ display: "none" }}
            />
            {showResetPopup && (
                <ResetPopup />
            )}
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
        const { enableDisplayGazeTracking, setEnableDisplayGazeTracking } = useNewTest();
        const { enableDisplayTimeLeft, setEnableDisplayTimeLeft } = useNewTest();
        const { secondsPerImage, setSecondsPerImage } = useNewTest();
        const { randomizeImageOrder, setRandomizeImageOrder } = useNewTest();

        function handleGazeTrackingChange() {
            setEnableDisplayGazeTracking(gazeTrackingInputRef.current.checked);
        }

        function handleDisplayTimeLeftChange() {
            setEnableDisplayTimeLeft(displayTimeInputRef.current.checked);
        }

        function handleSecondsPerImageChange() {
            setSecondsPerImage(Number(timeInputRef.current.value));
        }

        function handleRandomizeImageOrderChange() {
            setRandomizeImageOrder(randomizeImagesInputRef.current.checked);
        }

        return (
            <div className="new-test-control-panel">
                <h2>Settings</h2>
                <div className="new-test-control-checkbox">
                    <input 
                        type="checkbox" 
                        id="displayGaze" 
                        name="displayGaze"
                        ref={gazeTrackingInputRef}
                        onChange={handleGazeTrackingChange}
                        checked={enableDisplayGazeTracking} />
                    <label htmlFor="displayGaze"> Display Gaze Tracking </label>
                </div>
                <div className="new-test-control-checkbox">
                    <input 
                        type="checkbox" 
                        id="displayTimeLeft" 
                        name="displayTimeLeft"
                        ref={displayTimeInputRef}
                        onChange={handleDisplayTimeLeftChange}
                        checked={enableDisplayTimeLeft} />
                    <label htmlFor="displayTimeLeft"> Display Time Left </label>
                </div>
                <div className="new-test-control-checkbox">
                    <input 
                        className="new-test-time-input"
                        type="number" 
                        id="imageTime" 
                        name="imageTime"
                        ref={timeInputRef}
                        onBlur={handleSecondsPerImageChange}
                        defaultValue={secondsPerImage} />
                    <label htmlFor="imageTime"> seconds per image </label>
                </div>
                <div className="new-test-control-checkbox">
                    <input 
                        type="checkbox" 
                        id="randomizeImages" 
                        name="randomizeImages"
                        ref={randomizeImagesInputRef}
                        onChange={handleRandomizeImageOrderChange}
                        checked={randomizeImageOrder} />
                    <label htmlFor="randomizeImages"> Randomize Image Order </label>
                </div>
            </div>
        );
    }

    function ResetPopup() {
        const { resetNewTestContext } = useNewTest();

        return (
            <PopupMessage onClickOutside={() => setShowResetPopup(false)}>
                <div className="new-test-reset-popup-message-content">
                    <h2>Are you sure</h2>
                    <h2>you want to reset the test?</h2>
                    <div className="new-test-reset-popup-buttons-container">
                        <button 
                            onClick={() => setShowResetPopup(false)}>
                            Cancel
                        </button>
                        <button 
                            className="dangerous-button"
                            onClick={() => {
                                resetNewTestContext();
                                setShowResetPopup(false);
                            }}>
                            Reset
                        </button>
                    </div>
                </div>
            </PopupMessage>
        );
    }
}

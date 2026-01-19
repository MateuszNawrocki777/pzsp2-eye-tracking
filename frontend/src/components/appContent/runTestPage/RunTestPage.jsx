import { useRef, useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'

import { useNewTest } from '../../../hooks/newTestContext'

import getTestCall from '../../../services/api/getTestCall';
import getOnlineTestCall from '../../../services/api/getOnlineTestCall';

import TestResultImages from '../../testResultImages/TestResultImages';
import HeatmapPopup from '../../heatmap/HeatmapPopup';

import './RunTestPage.css'


export default function RunTestPage({ source }) {
    const {
            images: localImages,
            enableDisplayGazeTracking: localEnableDisplayGazeTracking,
            enableDisplayTimeLeft: localEnableDisplayTimeLeft,
            secondsPerImage: localSecondsPerImage,
            randomizeImageOrder: localRandomizeImageOrder,
        } = useNewTest();

    const [images, setImages] = useState([]);
    const [enableDisplayGazeTracking, setEnableDisplayGazeTracking] = useState(false);
    const [enableDisplayTimeLeft, setEnableDisplayTimeLeft] = useState(false);
    const [secondsPerImage, setSecondsPerImage] = useState(5);
    const [randomizeImageOrder, setRandomizeImageOrder] = useState(false);
    const [testName, setTestName] = useState("Local Test");
    const [testId, setTestId] = useState(null);

    const [popupImageIndex, setPopupImageIndex] = useState(null);

    let paramId = null;

    if (source !== 'local') {
        let { id } = useParams();
        paramId = id;
    }

    const windowRef = useRef(null);
    const [results, setResults] = useState([[0.3, 0.5], [0.6, 0.7]]); // For testing purposes, replace with null to disable

    useEffect(() => {
        if (source === 'local') {
            setImages(localImages.map(img => URL.createObjectURL(img)));
            setEnableDisplayGazeTracking(localEnableDisplayGazeTracking);
            setEnableDisplayTimeLeft(localEnableDisplayTimeLeft);
            setSecondsPerImage(localSecondsPerImage);
            setRandomizeImageOrder(localRandomizeImageOrder);
        }
        else if (source === 'my') {
            getTestCall(paramId).then((response) => {
                const testData = response.data;
                setImages(testData.fileLinks);
                setEnableDisplayGazeTracking(testData.dispGazeTracking);
                setEnableDisplayTimeLeft(testData.dispTimeLeft);
                setSecondsPerImage(testData.timePerImageMs / 1000);
                setRandomizeImageOrder(testData.randomizeOrder);
                setTestName(testData.title);
                setTestId(paramId);
            });
        }
        else if (source === 'online') {
            getOnlineTestCall(paramId).then((response) => {
                const testData = response.data;
                setImages(testData.fileLinks);
                setEnableDisplayGazeTracking(testData.dispGazeTracking);
                setEnableDisplayTimeLeft(testData.dispTimeLeft);
                setSecondsPerImage(testData.timePerImageMs / 1000);
                setRandomizeImageOrder(testData.randomizeOrder);
                setTestName(testData.title);
                setTestId(testData.id);
            });
        }
    }, [source, paramId]);

    useEffect(() => {
        window.addEventListener("message", handleMessage);
        return () => window.removeEventListener("message", handleMessage);
    }, [images, enableDisplayGazeTracking, enableDisplayTimeLeft, secondsPerImage, randomizeImageOrder]);


    return (
        <div className="run-test-page-container">
            <div className="run-test-content-container">
                <h1>{testName}</h1>
                {!results ? (
                    <div className="run-test-run-button-container">
                        <button onClick={handleRun} className='run-test-run-button'>Start Test</button>
                    </div>
                ) : (
                    <>
                        <TestResultImages images={images} setIndex={(index) => setPopupImageIndex(index)} />
                    </>
                )}
            </div>
            {popupImageIndex !== null && (
                <HeatmapPopup
                    image={images[popupImageIndex]}
                    points={results.map(([x, y]) => [x, y, 1])}
                    onClose={() => setPopupImageIndex(null)}
                />
            )}
        </div>
    )

    function handleRun() {
        const win = window.open("/run.html", "_blank");
        windowRef.current = win;
    };

    function handleMessage(event) {
        if (event.origin !== window.location.origin) return;
        if (event.source !== windowRef.current) return;

        switch (event.data.type) {
        case "getImages":
            {
            windowRef.current.postMessage({
                images,
                enableDisplayGazeTracking,
                enableDisplayTimeLeft,
                secondsPerImage,
                randomizeImageOrder
            }, window.location.origin);
            }
            break;
        case "postResults":
            setResults(event.data.results);
            console.log("Received results:", event.data.results);
            break;
        default:
            break;
        }

    };
}
import { useRef, useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'

import { useNewTest } from '../../../hooks/newTestContext'

import LoadingButton from '../../loadingButton/LoadingButton';

import getTestCall from '../../../services/api/getTestCall';
import getOnlineTestCall from '../../../services/api/getOnlineTestCall';
import submitStudyCall from '../../../services/api/submitStudyCall';

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
    const [results, setResults] = useState(null);

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
                        <div className="run-test-control-container">
                            {source !== 'local' && <SubmitTestResultsTile />}
                        </div>
                    </>
                )}
            </div>
            {popupImageIndex !== null && (
                <HeatmapPopup
                    image={images[popupImageIndex]}
                    points={results[popupImageIndex].map(([x, y]) => [x, y, 1])}
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

    function SubmitTestResultsTile() {
        const testSubmitionNameInputRef = useRef(null);
        const navigate = useNavigate();

        const [alreadySubmitted, setAlreadySubmitted] = useState(false);

        return (
            <div className="run-test-submit-results-tile">
                <div>
                    <h2>Submit Results</h2>
                    <p>Submit your test results to the server.</p>
                </div>
                <div className="run-test-submit-results-form">
                    {alreadySubmitted ? (
                        <p className="run-test-submit-results-submitted-message">
                            Results submitted! Thank you.
                        </p>
                    ) : (
                    <input
                        type="text"
                        placeholder="Enter test submission name"
                        ref={testSubmitionNameInputRef}
                    />
                    )}
                    <LoadingButton
                        className={`run-test-submit-results-button${alreadySubmitted ? " disabled" : ""}`}
                        onClick={() => {
                            if (alreadySubmitted) return;
                            const submissionName = testSubmitionNameInputRef.current.value;
                            submitStudyCall(testId, submissionName, results);
                            setAlreadySubmitted(true);
                        }}
                    >
                        Submit
                    </LoadingButton>
                </div>
            </div>
        );
    }
}

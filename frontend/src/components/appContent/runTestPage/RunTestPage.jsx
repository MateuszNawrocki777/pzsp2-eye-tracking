import { useRef, useState, useEffect } from 'react'

import { useNewTest } from '../../../hooks/newTestContext'

import Heatmap from '../../heatmap/Heatmap';

import './RunTestPage.css'


export default function RunTestPage() {
    // const { images } = useNewTest();
    const {
        images,
        enableDisplayGazeTracking,
        enableDisplayTimeLeft,
        secondsPerImage,
        randomizeImageOrder,
        testName,
    } = useNewTest();

    const windowRef = useRef(null);
    const [results, setResults] = useState(null);

    useEffect(() => {
        window.addEventListener("message", handleMessage);
        return () => window.removeEventListener("message", handleMessage);
    }, []);


    return (
        <div className="run-test-page-container">
            {!results ? (
            <button onClick={handleRun}>Start Test</button>
            ) : (
                <Heatmap image={images[0]} points={results} />
            )}
        </div>
    )

    function handleRun() {
        console.log("RUN clicked!");
        const win = window.open("/run.html", "_blank");
        windowRef.current = win;
    };

    function handleMessage(event) {
        if (event.origin !== window.location.origin) return;
        if (event.source !== windowRef.current) return;

        switch (event.data.type) {
        case "getImages":
            {
            console.log("Sending images to test window");
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
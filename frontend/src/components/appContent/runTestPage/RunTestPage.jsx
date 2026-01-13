import { useRef, useState, useEffect } from 'react'

import { useNewTest } from '../../../hooks/newTestContext'

import './RunTestPage.css'


export default function RunTestPage() {
    const { images } = useNewTest();
    const windowRef = useRef(null);
    const [results, setResults] = useState(null);

    useEffect(() => {
        window.addEventListener("message", handleMessage);
        return () => window.removeEventListener("message", handleMessage);
    }, []);

    {/* TODO: Probably disable the button until images are loaded */}
    {/* Also, change this so this doesnt always get the local test */}

    return (
        <div className="run-test-page-container">
            <button onClick={handleRun}>Start Test</button>
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
            const time = 10;
            console.log("Sending images to test window");
            windowRef.current.postMessage({ images, time }, window.location.origin);
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
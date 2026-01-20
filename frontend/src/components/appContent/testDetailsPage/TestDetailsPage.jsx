import { useState, useRef, useEffect, use } from "react";
import { useNavigate, useParams } from "react-router-dom";

import deleteTestCall from "../../../services/api/deleteTestCall";
import getTestCall from "../../../services/api/getTestCall";
import generateLinkCall from "../../../services/api/generateLinkCall";
import getMainHeatmapCall from "../../../services/api/getMainHeatmapCall";
import getStudiesCall from "../../../services/api/getStudiesCall";
import getStudyHeatmapCall from "../../../services/api/getStudyHeatmapCall";

import TestResultImages from "../../testResultImages/TestResultImages";
import PopupMessage from "../../popupMessage/PopupMessage";

import HeatmapPopup from "../../heatmap/HeatmapPopup";

import LoadingButton from "../../loadingButton/LoadingButton";


import "./TestDetailsPage.css";

export default function TestDetailsPage() {
  const { id: testId } = useParams();

  const navigate = useNavigate();

  const [images, setImages] = useState([]);

  const [showDeletePopup, setShowDeletePopup] = useState(false);
  const [popupImageIndex, setPopupImageIndex] = useState(null);

  const [testName, setTestName] = useState("Loading test...");
  const [secondsPerImage, setSecondsPerImage] = useState();
  const [randomizeImageOrder, setRandomizeImageOrder] = useState(false);
  const [enableDisplayGazeTracking, setEnableDisplayGazeTracking] =
      useState(false);
  const [enableDisplayTimeLeft, setEnableDisplayTimeLeft] = useState(false);

  const [mainHeatmap, setMainHeatmap] = useState(null);

  const [isLoadingStudies, setIsLoadingStudies] = useState(false);
  const [studiesError, setStudiesError] = useState(null);
  const [studies, setStudies] = useState([]);

  const [selectedStudyId, setSelectedStudyId] = useState(null);
  const [secondaryHeatmap, setSecondaryHeatmap] = useState(null);

  useEffect(() => {
    async function loadTest() {
      try {
        const response = await getTestCall(testId);
        const testData = response.data;

        setTestName(testData.title);
        setImages(testData.fileLinks);
        setSecondsPerImage(testData.timePerImageMs / 1000);
        setRandomizeImageOrder(testData.randomizeOrder);
        setEnableDisplayGazeTracking(testData.dispGazeTracking);
        setEnableDisplayTimeLeft(testData.enableDisplayTimeLeft);

      } catch (e) {
        console.error("Failed to load test", e);
        setTestName("Error loading test");
      }
    }

    loadTest();
  }, [testId]);

  useEffect(() => {
    async function loadMainHeatmap() {
      try {
        const response = await getMainHeatmapCall(testId);
        setMainHeatmap(response.data.heatmaps);
      } catch (e) {
        console.error("Failed to load main heatmap", e);
      }
    }

    loadMainHeatmap();
  }, [testId]);

  useEffect(() => {
    async function loadStudies() {
      setIsLoadingStudies(true);
      setStudiesError(null);
      try {
        const response = await getStudiesCall(testId);
        setStudies(response.data);
      } catch (e) {
        setStudiesError("Failed to load studies");
      } finally {
        setIsLoadingStudies(false);
      }
    }

    loadStudies();
  }, [testId]);

  useEffect(() => {
    if (selectedStudyId === null) {
      setSecondaryHeatmap(null);
      return;
    }

    async function loadSecondaryHeatmap() {
      try {
        const response = await getStudyHeatmapCall(selectedStudyId);
        setSecondaryHeatmap(response.data.heatmaps);
      } catch (e) {
        console.error("Failed to load secondary heatmap", e);
      }
    }

    loadSecondaryHeatmap();
  }, [selectedStudyId]);

  return (
    <div className="test-details-container">
      <div className="test-details-content-container">
        <div>
          <h1>{testName}</h1>
          <TestResultImages images={images} setIndex={(index) => setPopupImageIndex(index)} />
        </div>
        <div className="test-details-control-container">
          <Settings
            secondsPerImage={secondsPerImage}
            randomizeImageOrder={randomizeImageOrder}
            enableDisplayGazeTracking={enableDisplayGazeTracking}
            enableDisplayTimeLeft={enableDisplayTimeLeft} />
          <StudiesSection />
          <RunTest />
        </div>
        <button
          className="test-details-delete-button"
          onClick={() => setShowDeletePopup(true)}
        >
          Delete
        </button>
      </div>
      {showDeletePopup && <DeletePopup />}
      {popupImageIndex !== null && (
        <HeatmapPopup
          image={images[popupImageIndex]}
          points={selectedStudyId ?
                    secondaryHeatmap[popupImageIndex].map(({x, y, val}) => [x/384, y/216, val]) :
                    mainHeatmap[popupImageIndex].map(({x, y, val}) => [x/384, y/216, val])}  // TODO: replace 384 and 216 with 1
          onClose={() => setPopupImageIndex(null)}
        />
      )}
    </div>
  );

  function Settings({enableDisplayGazeTracking,
                     enableDisplayTimeLeft,
                     secondsPerImage,
                     randomizeImageOrder}) {
    return (
      <div className="test-details-control-panel">
        <h2>Settings</h2>
        <div className="test-details-control-checkbox">
          <input
            type="checkbox"
            id="displayGaze"
            name="displayGaze"
            checked={enableDisplayGazeTracking}
            disabled={true}
          />
          <label htmlFor="displayGaze"> Display Gaze Tracking </label>
        </div>
        <div className="test-details-control-checkbox" style={{ display: "none"}}>
          <input
            type="checkbox"
            id="displayTimeLeft"
            name="displayTimeLeft"
            checked={enableDisplayTimeLeft}
            disabled={true}
          />
          <label htmlFor="displayTimeLeft"> Display Time Left </label>
        </div>
        <div className="test-details-control-checkbox">
          <input
            className="test-details-time-input"
            type="number"
            id="imageTime"
            name="imageTime"
            defaultValue={secondsPerImage}
            disabled={true}
          />
          <label htmlFor="imageTime"> seconds per image </label>
        </div>
        <div className="test-details-control-checkbox" style={{ display: "none"}}>
          <input
            type="checkbox"
            id="randomizeImages"
            name="randomizeImages"
            checked={randomizeImageOrder}
            disabled={true}
          />
          <label htmlFor="randomizeImages"> Randomize Image Order </label>
        </div>
      </div>
    );
  }

  function DeletePopup() {

    return (
      <PopupMessage onClickOutside={() => setShowDeletePopup(false)}>
        <div className="test-details-delete-popup-message-content">
          <h2>Are you sure</h2>
          <h2>you want to delete the test?</h2>
          <div className="test-details-delete-popup-buttons-container">
            <button onClick={() => setShowDeletePopup(false)}>Cancel</button>
            <LoadingButton
              className="dangerous-button"
              onClick={() => {
                deleteTestCall(testId);
                navigate("/myTests");
              }}
            >
              Delete
            </LoadingButton>
          </div>
        </div>
      </PopupMessage>
    );
  }

  function RunTest() {

    const useCountRef = useRef(null);
    const expiryDateRef = useRef(null);
    const linkRef = useRef(null);

    return (
      <div className="test-details-control-panel">
        <h2>Run Test</h2>
        <button
            className={`test-details-run-test-button`}
            onClick={() => {navigate(`/runTest/my/${testId}`)}}
          >
            Run Test
        </button>
        <div className="test-details-run-test-control">

          <input
            type="number"
            placeholder="Max uses (optional)"
            ref={useCountRef}
            defaultValue={null}
            onBlur={() => {}}
          />
          <input
            type="text"
            ref={expiryDateRef}
            placeholder="Expiry date (optional)"
            defaultValue={null}
            onFocus={(e)=>(e.target.type="date")}
          />
        </div>
        <div className="test-details-run-test-generate-button-container">
          <input
            type="text"
            placeholder="Generated link"
            ref={linkRef}
            className="test-details-run-test-link"
            readOnly
          />
          <button onClick={generateLink}>Share</button>
        </div>
      </div>
    );

    function generateLink() {
        const expiryDate = expiryDateRef.current.value
            ? new Date(expiryDateRef.current.value + "T23:59:59.999Z").toISOString()
            : null;
        generateLinkCall(testId,
                       useCountRef.current.value,
                       expiryDate)
        .then((response) => {
            const link = `${import.meta.env.VITE_FRONTEND_URL}/runTest/online/${response.data.access_link}`;
            linkRef.current.value = link;
            navigator.clipboard.writeText(link);
        })
        .catch((error) => {
            console.error("Error generating link:", error);
        });
    }
  }

  function StudiesSection() {

    let content;
    if (isLoadingStudies) {
      content = <p className="test-details-studies-message">Loading studies...</p>;
    } else if (studiesError) {
      content = <p className="test-details-studies-message">{studiesError}</p>;
    } else if (studies.length === 0) {
      content = <p className="test-details-studies-message">No studies found.</p>;
    } else {
      content = (
      <div className="test-details-studies-list">
        {studies.map((study) => (
          <StudyItem key={study.session_id} study={study} />
        ))}
      </div>

      );
    }

    return (
      <div className="test-details-control-panel studies-section">
        <div>
          <h2>Test Submissions</h2>
          <p className="test-details-study-p">Select picture to view the heatmap.<br />Select study to view submission specific heatmaps.</p>
        </div>
        {content}
        <button className="test-details-study-section-reset-button"
          onClick={() => setSelectedStudyId(null)}>
            Reset
        </button>
      </div>
    );
  }

  function StudyItem({study}) {
    const date = new Date(study.completed_at);
    return (
      <div className={`test-details-study-item${selectedStudyId === study.session_id ? " selected" : ""}`}
           onClick={() => setSelectedStudyId(study.session_id)}>
        <h3>{study.name}</h3>
        <p>{date.toLocaleString("pl-PL")}</p>
      </div>
    );
  }
}

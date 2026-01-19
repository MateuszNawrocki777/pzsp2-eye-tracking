import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import getTestsCall from "../../../services/api/getTestsCall";

import ImageThumbnail from "../../thumbnails/ImageThumbnail";
import ThumbnailWithContent from "../../thumbnails/ThumbnailWithContent";

import "./MyTestsPage.css";

export default function MyTestsPage() {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function loadTests() {
      try {
        const response = await getTestsCall();
        setTests(response.data);
      } catch (e) {
        console.error("Failed to load tests", e);
        setError(e);
      } finally {
        setLoading(false);
      }
    }

    loadTests();
  }, []);

  function MyTestsContent() {
    if (loading) {
      return <p className="my-tests-message">Loading tests...</p>;
    }

    if (error) {
      return <p className="my-tests-message">Error loading tests: {error.message}</p>;
    }

    if (tests.length === 0) {
      return <p className="my-tests-message">No tests yet. <br /> Click "New Test" to create a test.</p>;
    }

    return (
      <div className="my-tests-list">
        {tests.map((test) => (
          <MyTestCard
            key={test.id}
            id={test.id}
            title={test.title}
            firstImageLink={test.firstImageLink}
          />
        ))}
      </div>
    );
  }

  return (
    <div className="my-tests-container">
      <div className="my-tests-content-container">
        <h1>My Tests</h1>
        <MyTestsContent />
      </div>
    </div>
  );
}


function MyTestCard({ id, title, firstImageLink }) {
  const navigate = useNavigate();

  const thumbnail = (<ImageThumbnail image={firstImageLink} alt={title} />)

  const content = (
    <>
      <div className="my-tests-test-title">{title}</div>
      <button className="my-tests-run-button" onClick={() => navigate(`/run-test/my/${id}`)}>Run</button>
      <button onClick={() => navigate(`/myTests/${id}`)}>Results</button>
    </>
  );

  return (
    <ThumbnailWithContent
      image={thumbnail}>
      {content}
    </ThumbnailWithContent>
  );
}

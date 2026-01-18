import { useEffect, useState } from "react";
import getTestsCall from "../../../services/api/getTestsCall";

import "./MyTestsPage.css";

export default function MyTestsPage() {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // useEffect(() => {
  //   async function loadTests() {
  //     try {
  //       const response = await getTestsCall();
  //       setTests(response.data);
  //     } catch (e) {
  //       console.error("Failed to load tests", e);
  //       setError(e);
  //     } finally {
  //       setLoading(false);
  //     }
  //   }

  //   loadTests();
  // }, []);

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
      <ul>
        {tests.map((test) => (
          <li key={test.id}>
            <img src={test.firstImageLink} alt={test.title} width={100} />
            {test.title}
          </li>
        ))}
      </ul>
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

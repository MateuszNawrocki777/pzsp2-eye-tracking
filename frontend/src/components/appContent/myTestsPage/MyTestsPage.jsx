import { useEffect, useState } from "react";
import getTestsCall from "../../../services/api/getTestsCall";

import "./MyTestsPage.css";

export default function MyTestsPage() {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);
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
      return <p>Loading tests...</p>;
    }

    if (error) {
      return <p>Error loading tests: {error.message}</p>;
    }

    if (tests.length === 0) {
      return <p>No tests yet</p>;
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
      <div></div>
      <h1>My Tests</h1>
      <MyTestsContent />
    </div>
  );
}

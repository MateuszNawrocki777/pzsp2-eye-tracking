import { useEffect, useState } from "react";
import getTestsCall from "../../../services/api/getTestsCall";

import "./MyTestsPage.css";

export default function MyTestsPage() {
  const [tests, setTests] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadTests() {
      try {
        const response = await getTestsCall();
        setTests(response.data);
      } catch (e) {
        console.error("Failed to load tests", e);
      } finally {
        setLoading(false);
      }
    }

    loadTests();
  }, []);

  if (loading) {
    return <p>Loading tests...</p>;
  }

  return (
    <div className="home-container">
      <h1>My Tests</h1>

      {tests.length === 0 ? (
        <p>No tests yet</p>
      ) : (
        <ul>
          {tests.map((test) => (
            <li key={test.id}>
              <img src={test.firstImageLink} alt={test.title} width={100} />
              {test.title}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

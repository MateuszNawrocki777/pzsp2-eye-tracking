import { BrowserRouter, Routes, Route } from "react-router-dom";

import { AuthProvider } from "./hooks/authContext.jsx";
import Header from "./components/header/Header";
import AppContent from "./components/appContent/AppContent.jsx";
import RunTestPage from "./components/appContent/runTestPage/RunTestPage.jsx";

import "./App.css";

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Header />
        <Routes>
          <Route path="*" element={<AppContent />} />
          <Route path="/run-test" element={<RunTestPage />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;

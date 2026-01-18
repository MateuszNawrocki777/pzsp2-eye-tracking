import { Routes, Route } from "react-router-dom";

import LoginPage from "./loginPage/LoginPage";
import RegisterPage from "./registerPage/RegisterPage";
import PageNotFound from "./pageNotFound/PageNotFound";
import NewTestPage from "./newTestPage/NewTestPage";
import AdminPage from "./adminPage/AdminPage";
import RunTestPage from "./runTestPage/RunTestPage";
import HomePage from "./homePage/HomePage";
import MyTestsPage from "./myTestsPage/MyTestsPage"

import "./AppContent.css";

import "./AppContent.css";

export default function AppContent() {
  return (
    <div className="app-content-container">
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/newTest" element={<NewTestPage />} />
        <Route path="/myTests" element={<MyTestsPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="/runTest" element={<RunTestPage />} />
        <Route path="*" element={<PageNotFound />} />
      </Routes>
    </div>
  );
}

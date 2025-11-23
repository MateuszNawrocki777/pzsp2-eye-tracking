import { Routes, Route } from 'react-router-dom'

import LoginPage from './loginPage/LoginPage'

import './AppContent.css'


export default function AppContent() {
    return (
        <div className="app-content-container">
            <Routes>
                { // TODO: Add routes
}
                <Route path="/" element={<div>Home page</div>} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/about" element={<div>About page</div>} />
                <Route path="/profile" element={<div>Profile page</div>} />
                <Route path="*" element={<div>404 — Not Found</div>} />
            </Routes>
        </div>
    )
}

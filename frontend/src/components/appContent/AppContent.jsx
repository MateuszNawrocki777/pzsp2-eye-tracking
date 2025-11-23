import { Routes, Route } from 'react-router-dom'

import LoginPage from './loginPage/LoginPage'
import RegisterPage from './registerPage/RegisterPage'
import PageNotFound from './pageNotFound/PageNotFound'

import './AppContent.css'


export default function AppContent() {
    return (
        <div className="app-content-container">
            <Routes>
                { // TODO: Add routes
}
                <Route path="/" element={<div>Home page</div>} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/about" element={<div>About page</div>} />
                <Route path="/newTest" element={<div>New Test page</div>} />
                <Route path="/myTests" element={<div>My Tests page</div>} />
                <Route path="*" element={<PageNotFound />} />
            </Routes>
        </div>
    )
}

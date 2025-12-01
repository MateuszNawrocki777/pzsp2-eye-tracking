import { Routes, Route } from 'react-router-dom'

import LoginPage from './loginPage/LoginPage'
import RegisterPage from './registerPage/RegisterPage'
import PageNotFound from './pageNotFound/PageNotFound'
import NewTestPage from './newTestPage/NewTestPage'
import AdminPage from './adminPage/AdminPage'

import './AppContent.css'


export default function AppContent() {
    return (
        <div className="app-content-container">
            <Routes>
                <Route path="/" element={<div>Home page</div>} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/about" element={<div>About page</div>} />
                <Route path="/newTest" element={<NewTestPage />} />
                <Route path="/myTests" element={<div>My Tests page</div>} />
                <Route path="/admin" element={<AdminPage />} />
                <Route path="*" element={<PageNotFound />} />
            </Routes>
        </div>
    )
}

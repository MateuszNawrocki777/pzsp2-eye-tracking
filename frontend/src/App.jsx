import { BrowserRouter } from 'react-router-dom'

import Header from './components/header/Header'
import AppContent from './components/appContent/AppContent.jsx'

import './App.css'

function App() {
    return (
        <BrowserRouter>
            <Header />
            <AppContent />
        </BrowserRouter>
    )
}

export default App

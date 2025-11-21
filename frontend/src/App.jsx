import { BrowserRouter } from 'react-router-dom'

import { AuthProvider } from './hooks/authContext.jsx'
import Header from './components/header/Header'
import AppContent from './components/appContent/AppContent.jsx'

import './App.css'

function App() {
    return (
        <AuthProvider>
        <BrowserRouter>
            <Header />
            <AppContent />
        </BrowserRouter>
        </AuthProvider>
    )
}

export default App

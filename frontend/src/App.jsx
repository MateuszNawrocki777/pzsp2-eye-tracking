import { BrowserRouter } from "react-router-dom";

import { AuthProvider } from "./hooks/authContext.jsx";
import { NewTestProvider } from "./hooks/newTestContext.jsx";
import Header from "./components/header/Header";
import AppContent from "./components/appContent/AppContent.jsx";

import "./App.css";

function App() {
  return (
    <AuthProvider>
      <NewTestProvider>
        <BrowserRouter>
          <Header />
          <AppContent />
        </BrowserRouter>
      </NewTestProvider>
    </AuthProvider>
  );
}

export default App;

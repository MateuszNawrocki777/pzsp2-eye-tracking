import { createContext, useContext, useState } from "react";

import loginCall from "../services/api/loginCall";


const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    let token = null;

    async function login(email, password) {
        try {
            await loginCall(email, password);
            setIsLoggedIn(true);
        } catch (error) {
            console.error("Login failed:", error);
            throw error;
        }
    }

    function logout() {
        setIsLoggedIn(false);
    }

    const value = {
        isLoggedIn,
        login,
        logout,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
    return ctx;
}

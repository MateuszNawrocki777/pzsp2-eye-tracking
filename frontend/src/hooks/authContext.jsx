import { createContext, useContext, useState } from "react";

import loginCall from "../services/api/loginCall";
import registerCall from "../services/api/registerCall";


const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [role, setRole] = useState("");

    async function login(email, password) {
        try {
            const response = await loginCall(email, password);

            const token = response.data.token;
            const role = response.data.role;

            console.log("role:", role);

            setRole(role);
            setIsLoggedIn(true);
        } catch (error) {
            console.error("Login failed:", error);
            throw error;
        }
    }

    async function register(email, password) {
        try {
            const response = await registerCall(email, password);

            const token = response.data.token;
            const role = response.data.role;

            console.log("role:", role);

            setRole(role);
            setIsLoggedIn(true);
        } catch (error) {
            console.error("Register failed:", error);
            throw error;
        }
    }

    function logout() {
        setRole("");
        setIsLoggedIn(false);
    }

    const value = {
        isLoggedIn,
        login,
        register,
        logout,
        role,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used within an AuthProvider");
    return ctx;
}

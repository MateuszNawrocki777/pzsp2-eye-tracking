import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { useAuth } from '../../../hooks/authContext.jsx'

import LoadingButton from '../../loadingButton/LoadingButton.jsx'

import './LoginPage.css'


export default function LoginPage() {
    const navigate = useNavigate();
    const { login } = useAuth();

    const usernameRef = useRef();
    const passwordRef = useRef();

    const [error, setError] = useState("");

    const handleLogin = async () => {
        const username = usernameRef.current.value;
        const password = passwordRef.current.value;
        try {
            await login(username, password);
        } catch (error) {
            setError("Login failed.");
            return;
        }
        navigate("/");
    }

    return (
        <div className="login-page-container">
            <h1>Login</h1>
            <div className='login-page-content'>
                <input type="text" placeholder="Username" ref={usernameRef} />
                <input type="password" placeholder="Password" ref={passwordRef} />
                <LoadingButton className='login-page-login-button' onClick={handleLogin}>Login</LoadingButton>
                <button onClick={() => {navigate("/register")}}>Register</button>
                <p className='login-page-error'>{error}</p>
            </div>
        </div>
    )
}
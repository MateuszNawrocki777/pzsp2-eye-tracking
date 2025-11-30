import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { useAuth } from '../../../hooks/authContext.jsx'

import './RegisterPage.css'


export default function RegisterPage() {
    const navigate = useNavigate();
    const { login } = useAuth();

    const usernameRef = useRef();
    const emailRef = useRef();
    const passwordRef = useRef();
    const confirmPasswordRef = useRef();

    const [error, setError] = useState("");

    const handleRegister = () => {
        const username = usernameRef.current.value;
        const email = emailRef.current.value;
        const password = passwordRef.current.value;
        const confirmPassword = confirmPasswordRef.current.value;
        // TODO: Implement register logic
        console.log("Registering with", username, password);
        setError("Example error for testing");
        login();
        navigate("/");
    }

    return (
        <div className="register-page-container">
            <h1>Register</h1>
            <div className='register-page-content'>
                <input type="text" placeholder="Username" ref={usernameRef} />
                <input type="email" placeholder="Email" ref={emailRef} />
                <input type="password" placeholder="Password" ref={passwordRef} />
                <input type="password" placeholder="Confirm Password" ref={confirmPasswordRef} />
                <button className='register-page-register-button' onClick={handleRegister}>Register</button>
                <button onClick={() => {navigate("/login")}}>Login</button>
                <p className='register-page-error'>{error}</p>
            </div>
        </div>
    )
}
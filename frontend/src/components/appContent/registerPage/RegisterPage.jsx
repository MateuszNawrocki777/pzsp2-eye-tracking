import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'

import { useAuth } from '../../../hooks/authContext.jsx'

import LoadingButton from '../../loadingButton/LoadingButton.jsx'

import './RegisterPage.css'


export default function RegisterPage() {
    const navigate = useNavigate();
    const { register } = useAuth();

    const usernameRef = useRef();
    const emailRef = useRef();
    const passwordRef = useRef();
    const confirmPasswordRef = useRef();

    const [error, setError] = useState("");

    const handleRegister = async () => {
        // const username = usernameRef.current.value;
        const email = emailRef.current.value;
        const password = passwordRef.current.value;
        const confirmPassword = confirmPasswordRef.current.value;

        if (!verifyInputs(email, password, confirmPassword)) {
            return;
        }

        try {
            await register(email, password);
        } catch (error) {
            if (error.response && error.response.status === 409) {
                setError("Email already in use");
            } else {
                setError("Registration failed");
            }
            return;
        }
        navigate("/");
    }

    return (
        <div className="register-page-container">
            <h1>Register</h1>
            <div className='register-page-content'>
                {/* <input type="text" placeholder="Username" ref={usernameRef} /> */}
                <input type="email" placeholder="Email" ref={emailRef} />
                <input type="password" placeholder="Password" ref={passwordRef} />
                <input type="password" placeholder="Confirm Password" ref={confirmPasswordRef} />
                <LoadingButton className='register-page-register-button' onClick={handleRegister}>Register</LoadingButton>
                <button onClick={() => {navigate("/login")}}>Login</button>
                <p className='register-page-error'>{error}</p>
            </div>
        </div>
    )

    function verifyInputs(email, password, confirmPassword) {
        if (password !== confirmPassword) {
            setError("Passwords do not match");
            return false;
        }

        if (password.length < 8) {
            setError("Password must be longer 8 characters long");
            return false;
        }

        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailPattern.test(email)) {
            setError("Invalid email address");
            return false;
        }

        return true;
    }
}
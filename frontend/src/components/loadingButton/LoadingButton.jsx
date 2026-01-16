import { useState } from "react";

import "./LoadingButton.css"


export default function LoadingButton({ onClick, children, ...props}) {
    const [isLoading, setIsLoading] = useState(false);

    async function handleClick()
    {
        setIsLoading(true);
        await onClick();
        setIsLoading(false);
    }

    return (
        <button onClick={handleClick} {...props}>
            {isLoading ?
                <div className="loading-button-loader-div">
                    <div className="loading-button-loader"></div>
                </div>
                :
                <>
                    {children}
                </>
            }
        </button>
    );
}

import { createContext, useContext, useState } from "react";

const NewTestContext = createContext(null);

export function NewTestProvider({ children }) {
    const [images, setImages] = useState([]);

    const value = {
        images,
        setImages,
    };

    return <NewTestContext.Provider value={value}>{children}</NewTestContext.Provider>;
}

export function useNewTest() {
    const ctx = useContext(NewTestContext);
    if (!ctx) throw new Error("useNewTest must be used within a NewTestProvider");
    return ctx;
}

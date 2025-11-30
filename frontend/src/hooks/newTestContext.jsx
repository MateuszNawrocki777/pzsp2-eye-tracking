import { createContext, useContext, useState } from "react";

const NewTestContext = createContext(null);

export function NewTestProvider({ children }) {
    const [images, setImages] = useState([]);
    const [enableDisplayGazeTracking, setEnableDisplayGazeTracking] = useState(true);
    const [enableDisplayTimeLeft, setEnableDisplayTimeLeft] = useState(true);
    const [secondsPerImage, setSecondsPerImage] = useState(10);
    const [randomizeImageOrder, setRandomizeImageOrder] = useState(false);

    const value = {
        images,
        setImages,
        enableDisplayGazeTracking,
        setEnableDisplayGazeTracking,
        enableDisplayTimeLeft,
        setEnableDisplayTimeLeft,
        secondsPerImage,
        setSecondsPerImage,
        randomizeImageOrder,
        setRandomizeImageOrder,
    };

    return <NewTestContext.Provider value={value}>{children}</NewTestContext.Provider>;
}

export function useNewTest() {
    const ctx = useContext(NewTestContext);
    if (!ctx) throw new Error("useNewTest must be used within a NewTestProvider");
    return ctx;
}

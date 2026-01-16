import "./PopupMessage.css";


export default function PopupMessage({ children, onClickOutside }) {
    return (
        <div className="popup-message-background"
            onClick={onClickOutside ? onClickOutside : null}>
            <div className="popup-message-container"
                onClick={(e) => e.stopPropagation()}>
                {children}
            </div>
        </div>
    );
}

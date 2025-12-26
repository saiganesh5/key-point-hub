import { useState } from "react";
import { API_BASE } from "./config";
import "./App.css";
function App() {
    const [file, setFile] = useState(null);
    const [result, setResult] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const pingBackend = async () => {
        try {
            const res = await fetch(`${API_BASE}/api/ping`);
            const text = await res.text();
            alert(text);
        } catch {
            alert("Backend not reachable");
        }
    };

    // 🔥 KEY CHANGE: reset state on file change
    const handleFileChange = (e) => {
        const selectedFile = e.target.files[0];

        setFile(selectedFile);

        // clear previous experiment
        setResult(null);
        setError(null);
        setLoading(false);
    };

    const uploadImage = async () => {
        if (!file) return;

        setLoading(true);
        setError(null);
        setResult(null);

        const formData = new FormData();
        formData.append("file", file);

        try {
            const res = await fetch(`${API_BASE}/api/poses`, {
                method: "POST",
                body: formData,
            });

            if (!res.ok) {
                throw new Error(`HTTP ${res.status}`);
            }

            const data = await res.json();
            setResult(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <header className="header">
                <div>
                    <h1 className="title">KeyPointHub</h1>
                    <p className="subtitle">Upload an image to detect keypoints with the backend API.</p>
                </div>
                <div className="btnRow">
                    <button className="btn1" onClick={pingBackend}>Ping Backend</button>
                </div>
            </header>

            <section className="card">
                <label htmlFor="fileInput" className="fileLabel">
                    <span className="fileLabelText">Choose an image</span>
                    <input
                        id="fileInput"
                        type="file"
                        accept="image/*"
                        onChange={handleFileChange}
                    />
                </label>

                {file && (
                    <div className="fileMeta">
                        <p><strong>Selected:</strong> {file.name}</p>
                    </div>
                )}

                <div className="actions">
                    <button className="btn1" onClick={uploadImage} disabled={loading || !file}>
                        {loading ? "Processing..." : "Upload Image"}
                    </button>
                </div>
            </section>

            {file && (
                <section className="card">
                    <h3 className="sectionTitle">Preview</h3>
                    <img
                        className="previewImage"
                        src={URL.createObjectURL(file)}
                        alt="Selected preview"
                    />
                </section>
            )}

            {error && (
                <div className="alert error">{error}</div>
            )}

            {result && (
                <section className="card">
                    <h3 className="sectionTitle">Result</h3>
                    <pre className="codeBlock">{JSON.stringify(result, null, 2)}</pre>
                </section>
            )}
        </div>
    );
}

export default App;

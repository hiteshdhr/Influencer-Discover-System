async function test() {
    console.log("Sending POST request to http://127.0.0.1:5005/api/discover");
    const response = await fetch("http://127.0.0.1:5005/api/discover", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            keywords: ["react", "web dev", "frontend"],
            brandContext: "Ed-tech platform helping students master frontend development, React JS tutorial, web development, career advice"
        })
    });
    const data = await response.json();
    console.log(JSON.stringify(data, null, 2));
}
test();

import process from "node:process";


export function checkApiKeys(req, res, next) {
    if (process.env.NODE_ENV === "development") return next()
    const apiKey = req.headers['x-api-key'];
    const secretKey = req.headers['x-secret-key'];
    if (!apiKey || !secretKey) {
        return res.status(401).json({message: 'Unauthorized'});
    }
    if (apiKey !== process.env.API_KEY || secretKey !== process.env.SECRET_KEY) {
        return res.status(401).json({message: 'Unauthorized'});
    }
    next();
}
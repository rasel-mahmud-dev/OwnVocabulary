
function getToken(req: { headers: { authorization?: string } }): string | null {
    const token = req.headers["authorization"]?.split?.("Bearer ")?.[1]
    if (!token) return null;
    return token
}

export default  getToken;
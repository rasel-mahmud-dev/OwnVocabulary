interface ResponseInit {
    status: number,
    headers:Record<string, string>
}

class Res {

    private _status: number | undefined

    json(data: any) {
        return new Response(JSON.stringify(data), {
            headers: {
                'Content-Type': 'application/json'
            },
            status: this._status
        })
    }

    status(status: number) {
        this._status = status
        return this
    }
}


export const res = new Res()

export default Res

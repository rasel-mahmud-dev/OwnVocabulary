
class CustomError extends  Error {
    errorCode: string
    details: any
    statusCode: number;
    constructor(message: string, errorCode: string,  statusCodeOrOption: number | Record<string, any>) {
        super(message);
        if(typeof statusCodeOrOption === "object") {
            this.statusCode = statusCodeOrOption.statusCode || 500;
            this.details = statusCodeOrOption;
        } else {
            this.statusCode = statusCodeOrOption;
        }
        this.errorCode = errorCode;
        Object.setPrototypeOf(this, CustomError.prototype);
    }
}

export default CustomError;
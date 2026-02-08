import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import timezone from "dayjs/plugin/timezone";

dayjs.extend(utc);
dayjs.extend(timezone);
dayjs.tz.setDefault("Asia/Dhaka");


const date = {
    format: (date: string | Date, format: string = "YYYY-MM-DD HH:mm:ss") => {
        return dayjs(date).tz("Asia/Dhaka").format(format);
    },
    parse: (dateString: string, format: string = "YYYY-MM-DD HH:mm:ss") => {
        return dayjs.tz(dateString, format, "Asia/Dhaka").toDate();
    },
    isValid: (date: string | Date) => {
        return dayjs(date).isValid();
    },
    now: () => {
        return dayjs().tz("Asia/Dhaka").valueOf();
    },
    date: () => {
        return dayjs().tz("Asia/Dhaka")
    },
    startOfDay: () => {
        return dayjs().tz("Asia/Dhaka").startOf('date').valueOf()
    }
}

export default date

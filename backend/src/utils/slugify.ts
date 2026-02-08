
export function slugify(title: string): string {
    return title
        .toLowerCase()              // Convert to lowercase
        .trim()                      // Remove leading and trailing spaces
        .replace(/[\s\W-]+/g, '-')   // Replace spaces and non-word characters with hyphens
        .replace(/^-+|-+$/g, '');    // Remove leading and trailing hyphens
}

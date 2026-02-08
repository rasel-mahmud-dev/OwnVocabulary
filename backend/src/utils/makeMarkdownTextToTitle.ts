
function makeMarkdownTextToTitle(markdownText: string) {
    if (!markdownText) return '';

    let title = markdownText?.length > 100
        ? markdownText.substring(0, 100)
        : markdownText;

      title = title
        // Remove Markdown headings (1-6 # symbols followed by space)
        .replace(/^#+\s+/g, '')
        // Remove Markdown emphasis (**, __, *, _)
        .replace(/(\*\*|__|\*|_)/g, '')
        // Remove Markdown links and images ([text](url))
        .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
        // Remove Markdown inline code (`code`)
        .replace(/`([^`]+)`/g, '$1')
        // Remove HTML tags if any
        .replace(/<[^>]+>/g, '')
        // Replace line breaks, tabs with single space
        .replace(/\s+/g, ' ')
        // Trim whitespace from both ends
        .trim();

    // Capitalize first letter of the title
    if (title.length > 0) {
        title = title.charAt(0).toUpperCase() + title.slice(1);
    }

    return title;
}

export default makeMarkdownTextToTitle

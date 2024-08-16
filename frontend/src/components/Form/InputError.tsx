const InputError = async ({ error }: { error: string }) => {
  const { ExclamationTriangleIcon } = await import(
    '@heroicons/react/24/outline'
  );
  return (
    <div role="alert" className="alert bg-warning shadow text-white flex">
      <ExclamationTriangleIcon className="size-8" />
      <span className="font-bold">{error}</span>
    </div>
  );
};

export default InputError;
